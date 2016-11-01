import socket
import sys
import random
import time
import string
import datetime

"""
Usage:

python tester.py [[IP] PORT]

python tester.py 23456 - connects to server localhost on port 23456

python tester.py 192.168.0.1 34567 - connects to server 192.168.0.1 on port 34567
"""

REGISTER_3 = 3.0 / 3
LOGIN_5 = 3.0 / 5
CREATE_AUCTION = 3.0
SEARCH_AUCTION_4 = 3.0 / 4
DETAIL_AUCTION_4= 3.0 / 4
MY_AUCTIONS_3 = 3.0 / 3
BID_4 = 3.0 / 4
EDIT_AUCTION_2 = 3.0 / 2
MESSAGE_2 = 3.0 / 2.0
OFFLINE_MESSAGE_NOTIFICATIONS_1 = 3.0
IMMEDIATE_MESSAGE_NOTIFICATIONS_2 = 3.0 / 2
ONLINE_USERS_1 = 3.0
IMMEDIATE_BID_NOTIFICATIONS_1 = 3.0
BID_END = 6.0

VERBOSE = False

IP = 'localhost'
PORT = 33333

if len(sys.argv) == 2:
    PORT = int(sys.argv[1])
if len(sys.argv) == 3:
    IP = sys.argv[1]
    PORT = int(sys.argv[2])

now = datetime.datetime.now()

def d(s):
    print s
    return s

class Client(object):
    def __init__(self, ip='localhost', port=33333, name='c'):
        self.ok = 'false'
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((ip, port))
        self.notifications = []
        self.name = name

    def log(self, msg):
        if VERBOSE:
            print msg

    def rq(self, msg):
        self.sock.sendall(msg + "\n")
        rcv = None
        while True:
            while not rcv:
                rcv = self.sock.recv(4096)

            if '\n' in rcv:
                parts = rcv.split("\n")
                process = parts[0]
                rcv = "\n".join(parts[1:])

            resp = self.handle(self.parse(process))
            if resp and 'type' in resp and resp['type'].startswith('notification_'):
                self.notifications.append(resp)
            elif resp:
                return resp

    def parse(self, msg):
        """ Converts protocol into dictionary """
        if not msg:
            return None
        try:
            return { p.split(":")[0].strip():p.split(":")[1].strip() for p in msg.split(",") }
        except:
            print "Error parsing:", msg
            return None

    def handle(self, data):
        if not data:
            return
        return data

    def close(self):
        self.sock.close()

class ScoreChecker(object):
    def __init__(self):
        self.positive = 0
        self.total = 0
        self.per_section = {}
        self.sections = []

    def __iadd__(self, a):
        section, points, cond = a[:]
        if section not in self.sections:
            self.sections.append(section)
        if section not in self.per_section:
            self.per_section[section] = 0
        self.total += points
        if cond:
            self.positive += points
            self.per_section[section] += points

        if VERBOSE:
            print section, ": ", self.per_section[section]
            print 20*"-"
        return self

    def summary(self):
        l = 50 * "=" + "\n"
        l += 'Grade summary:\n'
        l += 50 * "=" + "\n"
        l += "\n".join(["%s: %.1f" % (sec, self.per_section[sec]) for sec in self.sections ])
        return """%s
        Final Score: %.1f out of %.0f\n\n\n""" % (l, self.positive, self.total)

def random_string():
    return "".join([ random.choice(string.ascii_letters + string.digits) for i in range(random.randint(3,20))])

def random_code():
    return "".join([ random.choice(string.digits) for i in range(random.choice([10,13,]))])

if __name__ == '__main__':
    s = ScoreChecker()
    c1 = Client(ip=IP, port=PORT)
    c2 = Client(ip=IP, port=PORT, name='c2')
    c3 = Client(ip=IP, port=PORT, name='c2')

    random_user_1, random_pass_1 = random_string(), random_string()
    random_user_2, random_pass_2 = random_string(), random_string()
    random_user_3, random_pass_3 = random_string(), random_string()

    resp = c1.rq("""type: login, username: %s, password: %s """ % (random_user_1, random_pass_1))
    s += ("login", LOGIN_5, resp.get('ok', None) == "false")

    time.sleep(5)
    resp = c2.rq("""type: register, username: %s, password: %s """ % (random_user_1, random_pass_1))
    s += ("register", REGISTER_3, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c1.rq("""type: register, username: %s, password: %s """ % (random_user_2, random_pass_2))
    s += ("register", REGISTER_3, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c1.rq("""type: login, username: %s, password: %s """ % (random_user_1, random_pass_1))
    s += ("login", LOGIN_5, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c2.rq("""type: login, username: %s, password: %s """ % (random_user_2, random_pass_2))
    s += ("login", LOGIN_5, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c3.rq("""type: register, username: %s, password: %s """ % (random_user_3, random_pass_3))
    s += ("register", REGISTER_3, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c2.rq("""type: login, username: %s, password: %s """ % (random_user_3, random_pass_3))
    s += ("login", LOGIN_5, resp.get('ok', None) == "true")

    p1_code, p1_title, p1_desc, p1_amount = random_code(), random_string(), random_string(), random.randint(5,100)
    p1_deadline = now + datetime.timedelta(minutes=1)
    p1_deadline = p1_deadline.strftime('%Y-%m-%d %H-%M')

    time.sleep(5)
    resp = c1.rq("""type : search_auction , code:%s""" % p1_code)
    s += ("search auction", SEARCH_AUCTION_4, 'items_count' in resp and int(resp['items_count']) == 0)

    time.sleep(5)
    resp = c1.rq("""type : create_auction , code:%s, title : %s, description: %s , deadline : %s , amount : %d""" % (p1_code, p1_title, p1_desc, p1_deadline, p1_amount))
    s += ("create auction", CREATE_AUCTION, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c1.rq("""type : search_auction , code:%s""" % p1_code)
    s += ("search auction", SEARCH_AUCTION_4, 'items_count' in resp and int(resp['items_count']) > 0)
    s += ("search auction", SEARCH_AUCTION_4, 'items_0_code' in resp and resp['items_0_code'] == p1_code)
    s += ("search auction", SEARCH_AUCTION_4, 'items_0_title' in resp and resp['items_0_title'] == p1_title)
    p1_id = resp['items_0_id']

    time.sleep(5)
    resp = c1.rq("""type : detail_auction , id:%s""" % p1_id)
    s += ("detail auction", DETAIL_AUCTION_4, 'bids_count' in resp and int(resp['bids_count']) == 0)
    s += ("detail auction", DETAIL_AUCTION_4, 'messages_count' in resp and int(resp['messages_count']) == 0)
    s += ("detail auction", DETAIL_AUCTION_4, 'code' in resp and resp['code'] == p1_code)
    s += ("detail auction", DETAIL_AUCTION_4, 'title' in resp and resp['title'] == p1_title)

    time.sleep(5)
    resp = c1.rq("""type : my_auctions""")
    s += ("my auction", MY_AUCTIONS_3, 'items_count' in resp and int(resp['items_count']) == 1)
    s += ("my auction", MY_AUCTIONS_3, 'items_0_code' in resp and resp['items_0_code'] == p1_code)
    s += ("my auction", MY_AUCTIONS_3, 'items_0_title' in resp and resp['items_0_title'] == p1_title)

    time.sleep(5)
    resp = c2.rq("""type : bid, id: %s, amount: %f""" % (p1_id, p1_amount * 0.8))
    s += ("bid", BID_4, resp.get('ok', None) == 'true')
    time.sleep(5)
    resp = c2.rq("""type : bid, id: %s, amount: %f""" % (p1_id, p1_amount * 2))
    s += ("bid", BID_4, resp.get('ok', None) == 'false')
    time.sleep(5)
    resp = c2.rq("""type : bid, id: %s, amount: %f""" % (p1_id, p1_amount * 0.9))
    s += ("bid", BID_4, resp.get('ok', None) == 'false')

    time.sleep(5)
    resp = c1.rq("""type : my_auctions""")
    s += ("immediate bid notifications", IMMEDIATE_BID_NOTIFICATIONS_1, len(c1.notifications) >= 1)

    time.sleep(5)
    resp = c1.rq("""type : edit_auction, id: %s, amount: %f""" % (p1_id, p1_amount * 1.2))
    s += ("edit auction", EDIT_AUCTION_2, resp.get('ok', None) == 'true')

    p1_title, p1_code = random_string(), random_string()
    time.sleep(5)
    resp = c1.rq("""type : edit_auction, id: %s, title: %s, code: %s""" % (p1_id, p1_title, p1_code))
    s += ("edit auction", EDIT_AUCTION_2, resp.get('ok', None) == 'true')

    msg = random_string()
    time.sleep(5)
    resp = c1.rq("""type : message, id: %s, text: %s""" % (p1_id, msg))
    s += ("message", MESSAGE_2, resp.get('ok', None) == 'true')

    time.sleep(5)
    resp = c2.rq("""type : my_auctions""")
    s += ("immediate message notifications", IMMEDIATE_MESSAGE_NOTIFICATIONS_2,  any([ msg == notification['text'] for notification in c2.notifications if 'text' in notification ]))

    time.sleep(5)
    resp = c3.rq("""type : my_auctions""")
    s += ("immediate message notifications", IMMEDIATE_MESSAGE_NOTIFICATIONS_2,  all([ msg != notification['text'] for notification in c2.notifications if 'text' in notification ]))
    c2.close()

    time.sleep(5)
    resp = c1.rq("""type : bid, id: %s, amount: %f""" % (p1_id, p1_amount * 0.5))
    s += ("bid", BID_4, resp.get('ok', None) == 'true')

    c2 = Client(ip=IP, port=PORT, name='c2')
    time.sleep(5)
    resp = c2.rq("""type: login, username: %s, password: %s """ % (random_user_2, random_pass_2))
    s += ("login", LOGIN_5, resp.get('ok', None) == "true")

    time.sleep(5)
    resp = c2.rq("""type : my_auctions""")
    s += ("offline bid notifications", OFFLINE_MESSAGE_NOTIFICATIONS_1, len(c2.notifications) >= 1)

    time.sleep(5)
    resp = c1.rq("""type : detail_auction , id:%s""" % p1_id)
    s += ("message", MESSAGE_2, 'messages_count' in resp and int(resp['messages_count']) == 1)

    time.sleep(5)
    resp = c1.rq("""type : online_users""")
    s += ("online_users", ONLINE_USERS_1, 'users_count' in resp and int(resp['users_count']) == 3)

    print s.summary()
    print "Now waiting 1 minute for final...\n\n\n"
    time.sleep(60)

    resp = c2.rq("""type : bid, id: %s, amount: %f""" % (p1_id, p1_amount * 0.3))
    s += ("bid ending", BID_END, d(resp.get('ok', None)) == 'false')

    c1.close()
    c2.close()
    c3.close()

    print s.summary()
